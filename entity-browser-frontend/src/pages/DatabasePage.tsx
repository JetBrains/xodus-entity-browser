import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Database} from "../api/backend-types";
import store from "../store/store";
import {Typography} from "@material-ui/core";
import * as React from "react";
import {observable} from "mobx";
import {Link} from "react-router-dom";

type PageParams = {
  databaseId: string
}


class DatabasePageStore {
  @observable database: Database = store.databases[0];
  @observable q: string | null = null;
  @observable typeId: number = 0;
}

const localStore = new DatabasePageStore();

@observer
class DatabasePage extends BasePage<PageParams> {

  pageId = 'databases';

  componentDidMount(): Promise<void> {
    // @ts-ignore
    const id = this.props.match.params.databaseId;
    if (id) {
      localStore.database = store.databases.filter((it) => it.uuid === id)[0];
    } else {
      localStore.database = store.databases[0];
    }
    this.pageId = 'databases' + localStore.database.uuid;
    this.syncPage();
    return super.componentDidMount();
  }

  renderContent(): any {
    // const link = `/databases/${localStore.database.uuid}/entities/12-12`;
    const link = `/entities`;
    return (
        <div>
          <Typography variant={"h5"}>
            Database location is
          </Typography>
          <Typography variant={"h5"}>
            <Link to={link}>
              LINK TO ENTITY
            </Link>
          </Typography>
        </div>
    );
  }
}

export default DatabasePage;
