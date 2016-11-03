public interface SymbolTable<K extends Comparable<K>> {
  void insert(K key);
  void search(K key);
  void delete(K key);
}

