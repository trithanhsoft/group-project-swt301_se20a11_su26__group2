package com.swp391.coding_platform.service.contest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseScoreboardManager {

    private final Map<Integer, List<SseEmitter>> emittersMap = new ConcurrentHashMap<>();

    public SseEmitter createConnection(Integer contestId) {
        // Timeout 15 minutes (900_000 ms)
        SseEmitter emitter = new SseEmitter(900_000L);

        emittersMap.computeIfAbsent(contestId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(contestId, emitter));
        emitter.onTimeout(() -> removeEmitter(contestId, emitter));
        emitter.onError((e) -> removeEmitter(contestId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("handshake")
                    .data("Connected to contest " + contestId + " scoreboard stream"));
        } catch (IOException e) {
            removeEmitter(contestId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Integer contestId, SseEmitter emitter) {
        List<SseEmitter> list = emittersMap.get(contestId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersMap.remove(contestId);
            }
        }
    }

    public void broadcast(Integer contestId, Object payload) {
        List<SseEmitter> list = emittersMap.get(contestId);
        if (list == null || list.isEmpty()) {
            return;
        }

        log.info("Broadcasting scoreboard update to {} listeners for contest {}", list.size(), contestId);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("scoreboard-update")
                        .data(payload));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            list.removeAll(deadEmitters);
            if (list.isEmpty()) {
                emittersMap.remove(contestId);
            }
        }
    }
}
