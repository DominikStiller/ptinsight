export class ExpiringMap<K, V> extends Map<K, Record<V>> {
  // @ts-ignore
  set(
    key: K,
    value: V,
    duration: number,
    expireCallback: (key: K, value: V) => void = undefined
  ) {
    var record = new Record(value, duration);
    super.set(key, record);
    setTimeout((key: K) => this.evictEntry(key, expireCallback), duration, key);
  }

  private evictEntry(
    key: K,
    expireCallback: (key: K, value: V) => void = undefined
  ): void {
    const record = super.get(key);
    if (record && record.expired) {
      // Check is necessary since value might have been updated with newer expiration date
      this.delete(key);
      if (expireCallback) {
        expireCallback(key, record.data);
      }
    }
  }

  // @ts-ignore
  get(key: K) {
    const record = super.get(key);
    return record === undefined || record.expired ? undefined : record.data;
  }

  has(key: K) {
    return super.has(key) ? !super.get(key).expired : false;
  }

  // @ts-ignore
  *values(): IterableIterator<V> {
    for (let record of super.values()) {
      yield record.data;
    }
  }
}

class Record<T> {
  data: T;
  private expire: number;

  constructor(data: T, duration: number) {
    this.data = data;
    this.expire = duration ? Date.now() + duration : undefined;
  }

  get expired() {
    return this.expire ? this.expire <= Date.now() : false;
  }
}
