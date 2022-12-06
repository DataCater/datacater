package io.datacater.core.authentication;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Tenant {

  @Column(name = "tenant_id")
  final String tenant = "datacater";
}
