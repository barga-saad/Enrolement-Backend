package org.bcp.dlmsenrolment.excptions;

public class PartnerNotFoundException extends RuntimeException {
  public PartnerNotFoundException(String message) {
    super(message);
  }
}

