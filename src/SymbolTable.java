public interface SymbolTable<K extends Comparable<K>> {
  void insert(K key);
  ScapeGoatSymbolTable.Node search(K key);
  void delete(K key);
}

