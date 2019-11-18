export interface BaseEntity {
  id: string
}

export interface Database extends BaseEntity {
  uuid: string

  location: string,
  key: string | null,

  opened: boolean,
  readonly: boolean,
  watchReadonly: boolean,

  encrypted: boolean,
  encryptionProvider: "SALSA" | "CHACHA",
  encryptionKey: string | null,
  encryptionIV: string | null // js can't in Long

}

export interface ApplicationState {
  dbs: Database[],
  readonly: boolean
}


interface Named {
  name: string
}

export interface PropertyType {
  readonly: boolean
  clazz: string
  displayName: string
}

export interface EntityProperty extends Named {
  type: PropertyType,
  value: String | null
}

export interface EntityLink extends Named {
  id: String,
  typeId: number,
  type: string,
  label: string,
  notExists: boolean
}

export interface LinkPager extends Named {
  skip: number,
  top: number,
  totalCount: number
  entities: EntityLink[]
}

export interface EntityBlob extends Named {
  blobSize: number

}

export interface EntityView {
  id: string,
  type: string,
  label: string,
  typeId: number,
  properties: EntityProperty[],
  links: LinkPager[],
  blobs: EntityBlob[]
}

export interface EntityType extends Named {
  id: number
}

export interface SearchPager {
  items: EntityView[]
  totalCount: number
}

export interface ChangeSummaryAction<T> extends Named {
  newValue: T | null
}

export interface PropertiesChangeSummaryAction extends ChangeSummaryAction<EntityProperty> {
}

export interface LinkChangeSummaryAction extends ChangeSummaryAction<EntityLink> {
  oldValue: EntityLink | null
  totallyRemoved: boolean
}

export interface BlobChangeSummaryAction extends ChangeSummaryAction<EntityBlob> {
}

export interface ChangeSummary {
  properties: PropertiesChangeSummaryAction[],
  links: LinkChangeSummaryAction[],
  blobs: BlobChangeSummaryAction[]
}


export const isYoutrack = (database: Database) => {
  return database.key === 'teamsysdata';
};

export const isHub = (database: Database) => {
  return database.key === 'jetpass';
};



