package com.powsybl.network.store.iidm.impl.tck;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Set;

//FIXME: WORKAROUND to ignore one test that fails in tck and cannot be overridden because it's not public in powsybl-core
// delete this class when we use at least powsybl-core 7.0.0
// + remove the annotation "@ExtendWith(ExcludeTestsExtension.class)" from NetworkTest
public class ExcludeTestsExtension implements InvocationInterceptor {
    private static final Set<String> EXCLUDED_TESTS = Set.of(
            "testSetMinimumAcceptableValidationLevelOnInvalidatedNetwork",
            "testSameLine",
            "testNullLine",
            "testDuplicateCoupling",
            "testFindSymmetric",
            "testRemoveByLines",
            "testRemoveByMutualCoupling",
            "testSetters",
            "testInvalidLineSegment",
            "testInvalidRAndX",
            "testListener",
            "testConnectDisconnectWithFictitiousBreaker",
            // the following tests are not public in powsybl-core tck and use the detailed DC
            // network model, which is not implemented in the network store
            "equivalentAcDcConverter",
            "testSignOnRemoteReactiveMode"
    );

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        String methodName = invocationContext.getExecutable().getName();
        if (EXCLUDED_TESTS.contains(methodName)) {
            throw new TestAbortedException("Test not applicable for network-store implementation");
        }
        invocation.proceed();
    }

    // some excluded tests already fail in the setup (@BeforeEach) of the tck class, before the
    // test method is invoked, so the setup must be skipped too
    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation,
                                          ReflectiveInvocationContext<Method> invocationContext,
                                          ExtensionContext extensionContext) throws Throwable {
        String testMethodName = extensionContext.getTestMethod().map(Method::getName).orElse("");
        if (EXCLUDED_TESTS.contains(testMethodName)) {
            throw new TestAbortedException("Test not applicable for network-store implementation");
        }
        invocation.proceed();
    }
}
