/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.riak.utils;

import java.io.PrintStream;
import java.io.PrintWriter;

public class UncheckedInterruptedException extends RuntimeException {

    private final InterruptedException exception;

    public UncheckedInterruptedException(InterruptedException exception) {
        this.exception = exception;
    }

    public InterruptedException getCheckedException() {
        return exception;
    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return exception.getLocalizedMessage();
    }

    @Override
    public Throwable getCause() {
        return exception.getCause();
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return exception.initCause(cause);
    }

    @Override
    public void printStackTrace() {
        exception.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        exception.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        exception.printStackTrace(s);
    }

    @Override
    public Throwable fillInStackTrace() {
        return exception.fillInStackTrace();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return exception.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        exception.setStackTrace(stackTrace);
    }
}
