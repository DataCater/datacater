package io.datacater.core.tenantAwareness;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class TenantId {
  private String tenantId;

  public String get() {
    return ((tenantId == null) ? "DataCater" : tenantId);
  }
}
