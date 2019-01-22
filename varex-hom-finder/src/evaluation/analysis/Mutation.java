package evaluation.analysis;
import java.util.Collection;
import java.util.HashSet;

public final class Mutation {

	public final String name;
	public final String className;
	public final String methodName;
	public final Change change;
	public final Collection<String> failedTests = new HashSet<>();
	
	public Mutation(String name, String className, String methodName, String oldOperator, String newOperator) {
		this.name = name;
		this.className = className;
		this.methodName = methodName;
		this.change = ChangeFactory.getOrCreateChange(oldOperator, newOperator);
	}

	@Override
	public String toString() {
		return name + " @ " + className + "." + methodName + " " + change;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
		
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		return ((Mutation) o).name.equals(name);		
	}
	
	public void addFailedTest(String test) {
		failedTests.add(test);
	}
	
	
}
