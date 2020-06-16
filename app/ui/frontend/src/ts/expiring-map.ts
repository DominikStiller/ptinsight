export class ExpiringMap<K, V> extends Map<K, Entity<V>> {
  // @ts-ignore
  set(key: K, value: V, duration: number) {
    var entity = new Entity(value, duration);
    super.set(key, entity);
    if (duration)
      setTimeout(
        (key: any) => {
          const o = super.get(key);
          if (o && o.expired) {
            // Check is necessary since value might have been updated with newer expiratino date
            this.delete(key);
          }
        },
        duration,
        key
      );
  }

  // @ts-ignore
  get(key: K) {
    var entity = super.get(key);
    return entity === undefined || entity.expired ? undefined : entity.data;
  }
}

export class Entity<T> {
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
