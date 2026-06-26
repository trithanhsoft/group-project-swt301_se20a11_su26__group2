const express = require('express');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 9000;

app.get('/health', (req, res) => {
    res.json({ status: 'OK', ffmpeg: true });
});

app.post('/extract-audio', (req, res) => {
    const { videoUrl } = req.body;

    if (!videoUrl) {
        return res.status(400).json({ error: 'videoUrl is required' });
    }

    console.log(`Extracting audio from: ${videoUrl}`);

    const tempDir = path.join(__dirname, 'temp');
    if (!fs.existsSync(tempDir)) {
        fs.mkdirSync(tempDir);
    }

    const uniqueId = Date.now() + '_' + Math.random().toString(36).substring(2, 15);
    const outputAudioPath = path.join(tempDir, `${uniqueId}.mp3`);

    // Run ffmpeg command:
    // ffmpeg -y -i <videoUrl> -vn -acodec libmp3lame -ac 1 -ar 16000 -ab 32k <outputAudioPath>
    const ffmpeg = spawn('ffmpeg', [
        '-y',
        '-i', videoUrl,
        '-vn',
        '-acodec', 'libmp3lame',
        '-ac', '1',
        '-ar', '16000',
        '-ab', '32k',
        outputAudioPath
    ]);

    let errorOutput = '';
    ffmpeg.stderr.on('data', (data) => {
        errorOutput += data.toString();
    });

    ffmpeg.on('close', (code) => {
        if (code !== 0) {
            console.error(`FFmpeg failed with code ${code}. Error: ${errorOutput}`);
            return res.status(500).json({ error: 'FFmpeg extraction failed', details: errorOutput });
        }

        console.log(`Audio extracted successfully to ${outputAudioPath}`);

        res.sendFile(outputAudioPath, (err) => {
            // Delete temp file after sending to avoid filling disk space
            fs.unlink(outputAudioPath, (unlinkErr) => {
                if (unlinkErr) console.error(`Failed to delete temp file ${outputAudioPath}:`, unlinkErr);
            });

            if (err) {
                console.error('Error sending file:', err);
                if (!res.headersSent) {
                    res.status(500).send('Error sending file');
                }
            }
        });
    });
});

app.listen(PORT, () => {
    console.log(`FFmpeg API service listening on port ${PORT}`);
});
