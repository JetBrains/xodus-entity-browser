import {observer} from 'mobx-react';
import BasePage from './BasePage';
import store from '../store/store';
import {
  Card, CardContent,
  CardHeader,
  Grid,
  LinearProgress,
  Paper,
  TableBody,
  TableHead,
  TableRow,
  Typography
} from '@material-ui/core';
import * as React from 'react';
import {observable} from 'mobx';
import {Database, EntityView, keyInfo} from '../api/backend-types';
import api, {DatabaseApi} from '../api/api';

type PageParams = {
  databaseId: string
  entityId: string
}


class EntityPageStore {

  @observable database: Database = store.databases[0];
  @observable entityType: number = 0;
  @observable loading: boolean = true;

  // @ts-ignore
  @observable entity: EntityView = {};

  // @ts-ignore
  api: DatabaseApi = {}

  reset() {
    this.database = store.databases[0];
    this.entityType = 0;
    // @ts-ignore
    this.api = {}
    // @ts-ignore
    this.entity = {}
  }
}

const entityStore = new EntityPageStore();


@observer
class EntityPage extends BasePage {

  constructor(props: any) {
    super(props);
    entityStore.reset();
  }

  async componentDidMount(): Promise<void> {
    // @ts-ignore
    const dbId = this.props.match.params.databaseId;
    // @ts-ignore
    const entityId = this.props.match.params.entityId;

    if (dbId) {
      entityStore.database = store.databases.filter((it) => it.uuid === dbId)[0];
    } else {
      entityStore.database = store.databases[0];
    }
    entityStore.api = api.database(entityStore.database);


    if (!entityId) {
      // todo redirect to 404
    }

    entityStore.loading = true;
    try {
      entityStore.entity = await entityStore.api.entity(entityId);
    } catch (e) {
      // todo redirect to 404
    }
    this.withTitle(keyInfo(entityStore.database) + " " + entityStore.database.location + " /// " + entityStore.entity.label);
    entityStore.loading = false;
  }

  renderContent(): any {
    if (entityStore.loading) {
      return (<LinearProgress/>)
    }
    const entity = entityStore.entity
    return (
      <div className={"entity-view-panel"}>
        <Card>
          <CardHeader
            title={'Properties'}
          />
          <CardContent>
            <Grid container spacing={1} style={{marginBottom: 20}}>
              {entity.properties.map((property) => (
                <React.Fragment key={entity.id + '_property_' + property.name}>
                  <Grid item xs={4}>{property.name}</Grid>
                  <Grid item xs={1}>{property.type.displayName}</Grid>
                  <Grid item xs={7}>{property.value}</Grid>
                </React.Fragment>
              ))}
            </Grid>
          </CardContent>
        </Card>
        <Card>
          <CardHeader
            title={'Links'}
          />
          <CardContent>
            <Grid container spacing={1} style={{marginBottom: 20}}>
              {entity.links.map((link) => (
                <React.Fragment key={entity.id + '_property_' + link.name}>
                  <Grid item xs={4}>{link.name}</Grid>
                  <Grid item xs={7}>...</Grid>
                </React.Fragment>
              ))}
            </Grid>
          </CardContent>
        </Card>
        <Card>
          <CardHeader
            title={'Blobs'}
          />
          <CardContent>
            <Grid container spacing={1} style={{marginBottom: 20}}>
              {entity.blobs.map((blob) => (
                <React.Fragment key={entity.id + '_property_' + blob.name}>
                  <Grid item xs={4}>{blob.name}</Grid>
                  <Grid item xs={7}>{blob.blobSize}</Grid>
                </React.Fragment>
              ))}
            </Grid>
          </CardContent>
        </Card>
      </div>
    );
  }
}

export default EntityPage;
