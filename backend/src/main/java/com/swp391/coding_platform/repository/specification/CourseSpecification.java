package com.swp391.coding_platform.repository.specification;

import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class CourseSpecification {

    public static Specification<CourseEntity> isStatusActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), CourseStatus.APPROVED);
    }

    public static Specification<CourseEntity> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("shortDescription")), pattern)
            );
        };
    }

    public static Specification<CourseEntity> hasCategories(List<Long> categoryIds) {
        return (root, query, cb) -> {
            if (categoryIds == null || categoryIds.isEmpty()) return null;

            Join<CourseEntity, CategoryEntity> categoryJoin = root.join("categories", JoinType.INNER);

            if (query != null) {    
                query.distinct(true);
            }

            return categoryJoin.get("id").in(categoryIds);
        };
    }

    public static Specification<CourseEntity> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice != null && maxPrice != null) return cb.between(root.get("price"), minPrice, maxPrice);
            if (minPrice != null) return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<CourseEntity> hasRatingBetween(Double minRating, Double maxRating) {
        return (root, query, cb) -> {
            if (minRating == null && maxRating == null) return null;
            if (minRating != null && maxRating != null) return cb.between(root.get("averageRating"), minRating, maxRating);
            if (minRating != null) return cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
            return cb.lessThanOrEqualTo(root.get("averageRating"), maxRating);
        };
    }

    public static Specification<CourseEntity> hasTeacherName(String teacherName) {
        return (root, query, cb) -> {
            if (teacherName == null || teacherName.isBlank()) return null;
            return cb.like(cb.lower(root.get("instructor").get("fullName")), "%" + teacherName.toLowerCase() + "%");
        };
    }

}

