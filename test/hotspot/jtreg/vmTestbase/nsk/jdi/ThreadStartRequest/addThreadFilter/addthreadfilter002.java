/*
 * Copyright (c) 2001, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jdi.ThreadStartRequest.addThreadFilter;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.util.*;
import java.io.*;

/**
 * The test for the implementation of an object of the type     <BR>
 * ThreadStartRequest.                                          <BR>
 *                                                              <BR>
 * The test checks that results of the method                   <BR>
 * <code>com.sun.jdi.ThreadStartRequest.addThreadFilter()</code> <BR>
 * complies with its spec.                                      <BR>
 * <BR>
 * The cases to check are as follows:
 * (1) if this request is currently enabled,                    <BR>
 *   - addThreadFilter(ThreadReference) results in throwing     <BR>
 *     InvalidRequestStateException;                            <BR>
 *   - addThreadFilter(null) results in throwing                <BR>
 *     NullPointerException or InvalidRequestStateException;    <BR>
 * (2) if this request is currently disabled,                   <BR>
 *     addThreadFilter(null) results in throwing                <BR>
 *     NullPointerException.                                    <BR>
 * <BR>
 * The test has three phases and works as follows.              <BR>
 * <BR>
 * In first phase,                                                      <BR>
 * upon launching debuggee's VM which will be suspended,                <BR>
 * a debugger waits for the VMStartEvent within a predefined            <BR>
 * time interval. If no the VMStartEvent received, the test is FAILED.  <BR>
 * Upon getting the VMStartEvent, it makes the request for debuggee's   <BR>
 * ClassPrepareEvent with SUSPEND_EVENT_THREAD, resumes the VM,         <BR>
 * and waits for the event within the predefined time interval.         <BR>
 * If no the ClassPrepareEvent received, the test is FAILED.            <BR>
 * Upon getting the ClassPrepareEvent,                                  <BR>
 * the debugger sets up the breakpoint with SUSPEND_EVENT_THREAD        <BR>
 * within debuggee's special methodForCommunication().                  <BR>
 * <BR>
 * In second phase, the debugger creates ThreadStartRequest             <BR>
 * and performs the above checks.                                       <BR>
 * <BR>
 * In third phase, at the end of the test,                              <BR>
 * the debuggee changes the value of the "instruction"                  <BR>
 * to inform the debugger of checks finished, and both end.             <BR>
 * <BR>
 */

public class addthreadfilter002 extends JDIBase {

    public static void main (String argv[]) {

        int result = run(argv, System.out);

        if (result != 0) {
            throw new RuntimeException("TEST FAILED with result " + result);
        }
    }

    public static int run (String argv[], PrintStream out) {

        int exitCode = new addthreadfilter002().runThis(argv, out);

        if (exitCode != PASSED) {
            System.out.println("TEST FAILED");
        }
        return exitCode;
    }

    //  ************************************************    test parameters

    private String debuggeeName =
        "nsk.jdi.ThreadStartRequest.addThreadFilter.addthreadfilter002a";

    //====================================================== test program

    //------------------------------------------------------ methods

    private int runThis (String argv[], PrintStream out) {

        argsHandler     = new ArgumentHandler(argv);
        logHandler      = new Log(out, argsHandler);
        Binder binder   = new Binder(argsHandler, logHandler);

        waitTime        = argsHandler.getWaitTime() * 60000;

        try {
            log2("launching a debuggee :");
            log2("       " + debuggeeName);
            if (argsHandler.verbose()) {
                debuggee = binder.bindToDebugee(debuggeeName + " -vbs");
            } else {
                debuggee = binder.bindToDebugee(debuggeeName);
            }
            if (debuggee == null) {
                log3("ERROR: no debuggee launched");
                return FAILED;
            }
            log2("debuggee launched");
        } catch ( Exception e ) {
            log3("ERROR: Exception : " + e);
            log2("       test cancelled");
            return FAILED;
        }

        debuggee.redirectOutput(logHandler);

        vm = debuggee.VM();

        eventQueue = vm.eventQueue();
        if (eventQueue == null) {
            log3("ERROR: eventQueue == null : TEST ABORTED");
            vm.exit(PASS_BASE);
            return FAILED;
        }

        log2("invocation of the method runTest()");
        switch (runTest()) {

            case 0 :  log2("test phase has finished normally");
                      log2("   waiting for the debuggee to finish ...");
                      debuggee.waitFor();

                      log2("......getting the debuggee's exit status");
                      int status = debuggee.getStatus();
                      if (status != PASS_BASE) {
                          log3("ERROR: debuggee returned UNEXPECTED exit status: " +
                              status + " != PASS_BASE");
                          testExitCode = FAILED;
                      } else {
                          log2("......debuggee returned expected exit status: " +
                              status + " == PASS_BASE");
                      }
                      break;

            default : log3("ERROR: runTest() returned unexpected value");

            case 1 :  log3("test phase has not finished normally: debuggee is still alive");
                      log2("......forcing: vm.exit();");
                      testExitCode = FAILED;
                      try {
                          vm.exit(PASS_BASE);
                      } catch ( Exception e ) {
                          log3("ERROR: Exception : e");
                      }
                      break;

            case 2 :  log3("test cancelled due to VMDisconnectedException");
                      log2("......trying: vm.process().destroy();");
                      testExitCode = FAILED;
                      try {
                          Process vmProcess = vm.process();
                          if (vmProcess != null) {
                              vmProcess.destroy();
                          }
                      } catch ( Exception e ) {
                          log3("ERROR: Exception : e");
                      }
                      break;
            }

        return testExitCode;
    }


   /*
    * Return value: 0 - normal end of the test
    *               1 - ubnormal end of the test
    *               2 - VMDisconnectedException while test phase
    */

    private int runTest() {

        try {
            testRun();

            log2("waiting for VMDeathEvent");
            getEventSet();
            if (eventIterator.nextEvent() instanceof VMDeathEvent)
                return 0;

            log3("ERROR: last event is not the VMDeathEvent");
            return 1;
        } catch ( VMDisconnectedException e ) {
            log3("ERROR: VMDisconnectedException : " + e);
            return 2;
        } catch ( Exception e ) {
            log3("ERROR: Exception : " + e);
            return 1;
        }

    }


    private void testRun()
                 throws JDITestRuntimeException, Exception {

        eventRManager = vm.eventRequestManager();

        ClassPrepareRequest cpRequest = eventRManager.createClassPrepareRequest();
        cpRequest.setSuspendPolicy( EventRequest.SUSPEND_EVENT_THREAD);
        cpRequest.addClassFilter(debuggeeName);

        cpRequest.enable();
        vm.resume();
        getEventSet();
        cpRequest.disable();

        ClassPrepareEvent event = (ClassPrepareEvent) eventIterator.next();
        debuggeeClass = event.referenceType();

        if (!debuggeeClass.name().equals(debuggeeName))
           throw new JDITestRuntimeException("** Unexpected ClassName for ClassPrepareEvent **");

        log2("      received: ClassPrepareEvent for debuggeeClass");

        setupBreakpointForCommunication(debuggeeClass);

    //------------------------------------------------------  testing section

        log1("     TESTING BEGINS");


        for (int i = 0; ; i++) {

            vm.resume();
            breakpointForCommunication();
            ThreadReference mainThread = bpEvent.thread(); // bpEvent saved by breakpointForCommunication()

            int instruction = ((IntegerValue)
                               (debuggeeClass.getValue(debuggeeClass.fieldByName("instruction")))).value();

            if (instruction == 0) {
                vm.resume();
                break;
            }

            log1(":::::: case: # " + i);

            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ variable part

            log2("......setting up ThreadStartRequest");
            log2("...... ThreadStartRequest tsr1 = eventRManager.createThreadStartRequest();");
            ThreadStartRequest tsr1 = eventRManager.createThreadStartRequest();
            tsr1.addCountFilter(1);
            tsr1.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            tsr1.putProperty("number", "ThreadStartRequest1");

            log2("...... tsr1.enable();");
            tsr1.enable();

            try {
                log2("...... tsr1.addThreadFilter(mainThread);");
                log2("         InvalidRequestStateException is expected");
                tsr1.addThreadFilter(mainThread);
                log3("ERROR: no InvalidRequestStateException ");
                testExitCode = FAILED;
            } catch ( InvalidRequestStateException e ) {
                log2("          InvalidRequestStateException");
            } catch ( Exception e ) {
                log3("ERROR: unexpected Exception : " + e);
                testExitCode = FAILED;
            }
            try {
                log2("...... tsr1.addThreadFilter(null);");
                log2("         NullPointerException or InvalidRequestStateException is expected");
                tsr1.addThreadFilter(null);
                log3("ERROR: no Exception thrown");
                testExitCode = FAILED;
            } catch ( NullPointerException e ) {
                log2("          NullPointerException");
            } catch ( InvalidRequestStateException e ) {
                log2("          InvalidRequestStateException");
            } catch ( Exception e ) {
                log3("ERROR: unexpected Exception : " + e);
                testExitCode = FAILED;
            }

            log2("...... tsr1.disable()");
            tsr1.disable();

            try {
                log2("...... tsr1.addThreadFilter(null);");
                log2("         NullPointerException is expected");
                tsr1.addThreadFilter(null);
                log3("ERROR: no NullPointerException ");
                testExitCode = FAILED;
            } catch ( NullPointerException e ) {
                log2("          NullPointerException");
            } catch ( Exception e ) {
                log3("ERROR: unexpected Exception : " + e);
                testExitCode = FAILED;
            }
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        }
        log1("    TESTING ENDS");
        return;
    }

}
