/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.squid.api;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonar.check.Message;

import java.text.MessageFormat;
import java.util.Locale;

public class CheckMessage implements Message {

  private Integer line;
  private Double cost;
  private SourceCode sourceCode;
  private Object checker;
  private String defaultMessage;
  private Object[] messageArguments;
  private Boolean bypassExclusion;

  public CheckMessage(Object checker, String message, Object... messageArguments) {
    this.checker = checker;
    this.defaultMessage = message;
    this.messageArguments = messageArguments;
  }

  /**
   * @deprecated replaced by the other constructor since 2.12. See SONAR-2875.
   */
  @Deprecated
  public CheckMessage(CodeCheck checker, String message, Object... messageArguments) {
    this((Object)checker, message, messageArguments);
  }

  public void setSourceCode(SourceCode sourceCode) {
    this.sourceCode = sourceCode;
  }

  public SourceCode getSourceCode() {
    return sourceCode;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public Integer getLine() {
    return line;
  }

  public void setCost(double cost) {
    this.cost = cost;
  }

  public Double getCost() {
    return cost;
  }

  public void setBypassExclusion(boolean bypassExclusion) {
    this.bypassExclusion = bypassExclusion;
  }

  public boolean isBypassExclusion() {
    return bypassExclusion == null ? false : bypassExclusion;
  }

  public Object getChecker() {
    return checker;
  }

  public String getDefaultMessage() {
    return defaultMessage;
  }

  public Object[] getMessageArguments() {
    return messageArguments;
  }

  public String getText(Locale locale) {
    return formatDefaultMessage();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("source", sourceCode).append("checker", checker).append("msg", defaultMessage)
        .append("line", line).toString();
  }

  public String formatDefaultMessage() {
    if (messageArguments.length == 0) {
      return defaultMessage;
    } else {
      return MessageFormat.format(defaultMessage, messageArguments);
    }
  }

}
