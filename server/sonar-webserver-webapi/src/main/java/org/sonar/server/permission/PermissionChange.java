/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.permission;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.db.permission.OrganizationPermission;

import static java.util.Objects.requireNonNull;
import static org.sonar.core.util.stream.MoreCollectors.toList;
import static org.sonar.server.exceptions.BadRequestException.checkRequest;

public abstract class PermissionChange {

  public enum Operation {
    ADD, REMOVE
  }

  private final Operation operation;
  private final String permission;
  private final ProjectUuid projectUuid;
  protected final PermissionService permissionService;

  public PermissionChange(Operation operation, String permission, @Nullable ProjectUuid projectUuid, PermissionService permissionService) {
    this.operation = requireNonNull(operation);
    this.permission = requireNonNull(permission);
    this.projectUuid = projectUuid;
    this.permissionService = permissionService;
    if (projectUuid == null) {
      checkRequest(permissionService.getAllOrganizationPermissions().stream().anyMatch(p -> p.getKey().equals(permission)),
        "Invalid global permission '%s'. Valid values are %s", permission,
        permissionService.getAllOrganizationPermissions().stream().map(OrganizationPermission::getKey).collect(toList()));
    } else {
      checkRequest(permissionService.getAllProjectPermissions().contains(permission), "Invalid project permission '%s'. Valid values are %s", permission,
        permissionService.getAllProjectPermissions());
    }
  }

  public Operation getOperation() {
    return operation;
  }

  public String getPermission() {
    return permission;
  }

  @CheckForNull
  public ProjectUuid getProject() {
    return projectUuid;
  }

  @CheckForNull
  public String getProjectUuid() {
    return projectUuid == null ? null : projectUuid.getUuid();
  }
}
