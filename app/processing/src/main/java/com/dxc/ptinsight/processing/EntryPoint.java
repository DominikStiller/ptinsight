package com.dxc.ptinsight.processing;

import com.dxc.ptinsight.processing.jobs.ArrivalCountJob;

public class EntryPoint {

  public static void main(String[] args) throws Exception {
    new ArrivalCountJob().execute();
  }
}
