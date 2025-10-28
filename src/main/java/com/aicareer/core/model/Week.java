package com.aicareer.core.model;

//import com.aicareer.module.course.DistributionByWeek;

import java.util.List;

public class Week{
  public String responseFromGpt;
  private int sid;
  public Week(String responseFromGpt, int sid) {
    this.responseFromGpt = responseFromGpt;
    this.sid = sid;
  }

  public int getSid() {
    return sid;
  }

  public String getResponseFromGpt() {
    return responseFromGpt;
  }

  public void setSid(int sid) {
    this.sid = sid;
  }

  public void setResponseFromGpt(String responseFromGpt) {
    this.responseFromGpt = responseFromGpt;
  }
}