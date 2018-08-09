package util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import benchmark.BenchmarkedVarexSSHOMFinder;
import cmu.conditional.Conditional;
import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_ConsoleOutputStream;

public class SSHOMRunner {
  private       ConditionalMutationWrapper targetClasses;
  private final Class[]                    testClasses;

  public SSHOMRunner(Class targetClass, Class testClass) {
    this.targetClasses = new ConditionalMutationWrapper(targetClass);
    this.testClasses = new Class[] { testClass };
  }

  public SSHOMRunner(Class[] targetClasses, Class[] testClasses) {
    this.targetClasses = new ConditionalMutationWrapper(targetClasses);
    this.testClasses = Arrays.copyOf(testClasses, testClasses.length);
  }

  String currentMutant = "";
  public SSHOMListener runJunitOnHOMAndFOMs(String... mutants)
      throws IllegalAccessException, NoSuchFieldException {
    JUnitCore jUnitCore = new JUnitCore();
    SSHOMListener sshomListener = runJunitOnHOM(mutants);
    jUnitCore.addListener(sshomListener);

    Map<String, FeatureExpr> expressions = JPF_gov_nasa_jpf_ConsoleOutputStream.testExpressions;
    jUnitCore.addListener(new RunListener() {
    	
    	boolean failed = false;
    	
    	@Override
    	public void testFinished(Description description) throws Exception {
    		super.testFinished(description);
    		if (!failed) { 
	    		FeatureExpr ctx = expressions.get(description.getMethodName());
	    		FeatureExpr feature = Conditional.createFeature(currentMutant);
	    		FeatureExpr fullSelection = feature;
	    		for (FeatureExpr f : BenchmarkedVarexSSHOMFinder.mutantExprs) {
	    			if (feature != f) {
	    				fullSelection = fullSelection.and(f.not());
	    			}
				}
	    		boolean correctFOM = Conditional.isContradiction(Conditional.and(ctx, fullSelection));
				if (!correctFOM) {
					System.out.println("failed " + Conditional.getCTXString(feature) +  " for test " + description.getClassName() + "." + description.getMethodName());
//					System.exit(-1);
				}
    		}
    		failed = false;
    	}
    	
    	@Override
    	public void testFailure(Failure failure) throws Exception {
    		super.testFailure(failure);
    		failed = true;
    		FeatureExpr ctx = expressions.get(failure.getDescription().getMethodName());
    		FeatureExpr feature = Conditional.createFeature(currentMutant);
    		FeatureExpr fullSelection = feature;
    		for (FeatureExpr f : BenchmarkedVarexSSHOMFinder.mutantExprs) {
    			if (!feature.equals(f)) {
    				fullSelection = fullSelection.and(Conditional.not(f));
    			}
			}
    		boolean correctFOM = !Conditional.isContradiction(Conditional.and(ctx, fullSelection));
			if (!correctFOM) {
				System.out.println("failed " + Conditional.getCTXString(feature) +  " for test " + failure.getDescription().getClassName() + "." + failure.getDescription().getMethodName());
//				System.exit(-1);
			}
    	}
    });
    
    //foms
    for (String s : mutants) {
      targetClasses.resetMutants();
      targetClasses.setMutant(s);
      currentMutant = s;
      sshomListener.signalFOMBegin();
      jUnitCore.run(testClasses);
      sshomListener.signalFOMEnd();
    }
    return sshomListener;
  }

  public SSHOMListener runJunitOnHOM(String... mutants)
      throws IllegalAccessException, NoSuchFieldException {
    JUnitCore jUnitCore = new JUnitCore();
    SSHOMListener sshomListener = new SSHOMListener();
    jUnitCore.addListener(sshomListener);
    // hom
    targetClasses.resetMutants();
    for (String s : mutants) {
      targetClasses.setMutant(s);
    }
    sshomListener.signalHOMBegin();
    jUnitCore.run(testClasses);
    sshomListener.signalHOMEnd();

    return sshomListener;
  }


  public Collection<String> getMutants() {
    return targetClasses.getMutants();
  }
}
