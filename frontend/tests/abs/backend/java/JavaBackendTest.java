/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.java;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import abs.ABSTest;
import abs.backend.java.codegeneration.JavaCode;
import abs.backend.java.codegeneration.JavaCodeGenerationException;
import abs.backend.java.scheduling.RandomSchedulingStrategy;
import abs.frontend.analyser.SemanticErrorList;
import abs.frontend.ast.Model;
import abs.frontend.parser.Main;
import static abs.ABSTest.Config.*;

public class JavaBackendTest extends ABSTest {

    private static final boolean DEBUG = false;

    final protected List<String> jvmArgs = new ArrayList<String>() {{ add("-Dabs.terminateOnException=true"); }};
    /**
     * Additional arguments when running the compiled ABS program.
     */
    final protected List<String> absArgs = new ArrayList<String>();

    public JavaBackendTest() {}

    public JavaBackendTest(long randomSeed) {
        jvmArgs.add("-Dabs.totalscheduler="+RandomSchedulingStrategy.class.getName());
        jvmArgs.add("-Dabs.randomseed="+randomSeed);
    }

    void assertValidStdLib(String absCode) throws Exception {
        assertValidJava(getJavaCode("module JavaUnitTest; " + absCode, true));
    }

    void assertValid(String absCode) throws Exception {
        assertValidJava(getJavaCode("module JavaUnitTest; " + absCode, false));
    }

    protected void assertValidJavaFile(String absFile, boolean useStdLib) throws Exception {
        Model m = assertParseFileOk(absFile, WITH_STD_LIB, TYPE_CHECK);
        assertValidJava(getJavaCode(m));
    }

    void assertValidJava(JavaCode javaCode) throws Exception {
        try {
            javaCode.compile("-classpath", "bin", "-d", javaCode.getSrcDir().getAbsolutePath()+"/gen/test");
        } catch (Exception e) {
            System.out.println(javaCode);
            throw e;
        } finally {
            javaCode.deleteCode();
        }
    }

    static class NoTestResultFoundException extends RuntimeException {
        NoTestResultFoundException() {
            super("No test result was found!");
        }
    }

    StringBuffer runJava(JavaCode javaCode, String... jvmargs) throws Exception {
        StringBuffer output = new StringBuffer();
        javaCode.compile("-classpath", "bin", "-d", javaCode.getSrcDir().getAbsolutePath()+"/gen/test");

        ArrayList<String> args = new ArrayList<String>();
        args.add("java");
        args.addAll(Arrays.asList(jvmargs));
        args.addAll(Arrays.asList("-cp", "bin" + File.pathSeparator + javaCode.getSrcDir().getAbsolutePath()+"/gen/test", javaCode.getFirstMainClass()));
        args.addAll(absArgs);
        ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[0]));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while (true) {
            String s;
            s = r.readLine();
            if (s == null)
                break;
            output.append(s + "\n");
        }
        r.close();
        javaCode.deleteCode();
        return output;
    }

    boolean runJavaAndTestResult(JavaCode javaCode, boolean expectFail) throws Exception {
        StringBuffer output = null;
        try {
            output = runJava(javaCode, jvmArgs.toArray(new String[0]));
            String s = output.toString() + "\n";
            String result = null;
            Pattern p = Pattern.compile(".*__ABS_TESTRESULT=([^\n]*)\n.*", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = p.matcher(s);
            if (m.matches()) {
                result = m.group(1);
            }

            if (result == null)
                throw new NoTestResultFoundException();

            return Boolean.valueOf(result);
        } catch (NoTestResultFoundException e) {
            if (expectFail) {
                throw e;
            } else {
                System.err.println(output.toString());
                //System.out.println(javaCode);
                return false;
            }
        } catch (Exception e) {
            if (output != null)
                System.err.println(output.toString());
            else
                System.err.println("NO OUTPUT");
            //System.err.println(javaCode);
            throw e;
        }
    }

    protected JavaCode getJavaCode(String absCode, boolean withStdLib) throws Exception {
        Model model = null;
        String code = null;
        code = absCode;
        // if (withStdLib)
        // code =
        // "data Unit = Unit; data Bool = True | False; data Int; data String; data Fut<A>; "
        // + code;
        model = Main.parseString(code, withStdLib);
        if (model.hasErrors()) {
            Assert.fail(model.getErrors().get(0).getHelpMessage());
        } else {
            SemanticErrorList el = model.typeCheck();
            if (!el.isEmpty()) {
                Assert.fail(el.get(0).getMsg());
            }
        }

        if (model.hasErrors()) {
            Assert.fail(model.getErrors().getFirst().getHelpMessage());
            return null;
        }
        return getJavaCode(model);
    }

    private JavaCode getJavaCode(Model model) throws IOException, JavaCodeGenerationException {
        JavaCode code = new JavaCode();
        model.generateJavaCode(code);
        return code;
    }

    private static File getTempFile(String testCode) throws IOException {
        File tmpFile = File.createTempFile("abs", "test");
        PrintWriter p = new PrintWriter(new FileOutputStream(tmpFile));
        p.print(testCode);
        p.close();
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    void assertEvalTrue(String absCode) throws Exception {
        assertEvalEquals(absCode, true);
    }

    public void assertEvalEquals(String absCode, boolean value) throws Exception {
        JavaCode javaCode = getJavaCode(absCode, true);
        if (DEBUG)
            System.err.println(javaCode);
        boolean res = runJavaAndTestResult(javaCode, false);
        if (value != res) {
            //System.out.println(javaCode);
        }
        Assert.assertEquals(value, res);
    }

    public void assertEvalFails(String absCode) throws Exception {
        JavaCode javaCode = getJavaCode(absCode, true);
        try {
            runJavaAndTestResult(javaCode, true);
            System.err.println(javaCode);
            Assert.fail("Expected that Java run failed, but did not.");
        } catch (NoTestResultFoundException e) {
            // OK
        }
    }

}
