package evaluation.analysis;
import java.util.HashMap;
import java.util.Map;

public final class Change {
	
	private static int ID = 0;
	public final int id = ID++;

	public final String oldOperator;
	public final String newOperator;
	
	protected Change(String oldOperator, String newOperator) {
		this.oldOperator = oldOperator;
		this.newOperator = newOperator;
	}
	
	@Override
	public String toString() {
		return oldOperator + "to" + newOperator;
	}
	
}
class ChangeFactory {
	private static final Map<String, Map<String, Change>> changeMap = new HashMap<>();
	
	private ChangeFactory() {}
	
	public static Change getOrCreateChange(String oldOperator, String newOperator) {
		Map<String, Change> map = changeMap.computeIfAbsent(oldOperator, v -> new HashMap<>());
		return map.computeIfAbsent(newOperator, v -> new Change(oldOperator, newOperator));
	}
	
	
	
}
