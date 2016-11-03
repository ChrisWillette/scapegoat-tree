public interface SymbolTable<K extends Comparable<K>> {
    public void insert(K key);
    public V search(K key);
    public V delete(K key)
}

