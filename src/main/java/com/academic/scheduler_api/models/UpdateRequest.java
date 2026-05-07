package com.academic.scheduler_api.models;

/**
 * POST /api/schedule/update için istek gövdesi.
 *
 * Örnek JSON:
 *   { "type": "enrollment", "entityId": 0, "courseId": 3 }
 *   { "type": "instructor", "entityId": 2, "courseId": 0 }
 */
public class UpdateRequest {

    /** "enrollment" veya "instructor" */
    private String type;

    /** Değişen eğitmen ID (type=instructor ise kullanılır) */
    private int entityId;

    /** Etkilenen ders ID */
    private int courseId;

    public UpdateRequest() {}

    public String getType()     { return type; }
    public int    getEntityId() { return entityId; }
    public int    getCourseId() { return courseId; }

    public void setType(String type)      { this.type = type; }
    public void setEntityId(int entityId) { this.entityId = entityId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
}
