import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Database, EntityView} from "../api/backend-types";
import store from "../store/store";
import {RouteChildrenProps} from "react-router";
import {Typography} from "@material-ui/core";
import * as React from "react";
import {observable} from "mobx";
import routerStore from '../store/router-store';

type PageParams = {
  databaseId: string
  entityId: string
}


class DatabasePageStore {
  @observable
  database: Database = store.databases[0];
  @observable
  entity: EntityView | null = null;
}

const localStore = new DatabasePageStore();


@observer
class EntityPage extends BasePage<{}> {

  entityId: string;

  // pageId = 'entity from ' + localStore.database.uuid;

  constructor(props: RouteChildrenProps<PageParams>) {
    super(props);
    const id = routerStore.location.pathname;
    const entityId = props.match?.params.entityId;
    this.entityId = entityId ? entityId : '';
    if (id) {
      localStore.database = store.databases.filter((it) => it.uuid === id)[0];
    }
    // this.pageId = 'entity ' + this.entityId + ' from ' + localStore.database.uuid;
  }

  async doLoad(): Promise<void> {
    this.syncPage();
  }

  renderContent(): any {
    return (
        <Typography variant={"h3"}>
          Entity {this.entityId}
        </Typography>
    );
  }
}

export default EntityPage;
