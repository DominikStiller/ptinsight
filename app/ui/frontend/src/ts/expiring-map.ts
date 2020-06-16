export class ExpiringMap extends Map {
  // @ts-ignore
  set(key: any, value: any, duration: number) {
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

  get(key: any) {
    var entity = super.get(key);
    return entity === undefined || entity.expired ? undefined : entity.data;
  }
}

export class Entity {
  private data: any;
  private expire: number;

  constructor(data: any, duration: number) {
    this.data = data;
    this.expire = duration ? Date.now() + duration : undefined;
  }

  get expired() {
    return this.expire ? this.expire <= Date.now() : false;
  }
}
