// com.aicareer.module.course.AssemblePlan
package com.aicareer.module.course;

import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.DTO.ResponseByWeek;

public interface AssemblePlan {
  ResponseByWeek assemblePlan(CourseRequest request);
}