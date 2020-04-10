import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Database, EntityType, keyInfo} from "../api/backend-types";
import store from "../store/store";
import {Grid, NoSsr, Paper, TextField} from "@material-ui/core";
import * as React from "react";
import {observable} from "mobx";
import api, {DatabaseApi} from "../api/api";
import * as queryString from "querystring";
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import HelpIcon from '@material-ui/icons/Help';
import Autocomplete from '@material-ui/lab/Autocomplete';


class DatabasePageStore {
  @observable database: Database = store.databases[0];
  @observable types: EntityType[] = [];
  @observable q: string = '';

  // @ts-ignore
  @observable selectedType: EntityType = {};
  // @ts-ignore
  api: DatabaseApi = {}
}

const localStore = new DatabasePageStore();

@observer
class DatabasePage extends BasePage<any> {

  pageId = 'Database';

  async componentDidMount(): Promise<void> {
    // @ts-ignore
    const id = this.props.match.params.databaseId;

    if (id) {
      localStore.database = store.databases.filter((it) => it.uuid === id)[0];
    } else {
      localStore.database = store.databases[0];
    }
    localStore.api = api.database(localStore.database);
    this.pageId = keyInfo(localStore.database) + " " + localStore.database.location;
    this.syncPage();
    await this.setupFromQueryParams();
    return super.componentDidMount();
  }

  async setupFromQueryParams() {
    const holdMyBeer = (param: string | string[], def: string) => (param || def).toString();

    let search = this.props.location.search;
    const params = queryString.parse(search ? search.substring(1, search.length) : search);
    const typeId = parseInt(holdMyBeer(params.typeId, "-1"));
    const q = holdMyBeer(params.q, "");

    localStore.types = await localStore.api.entityTypes();
    localStore.selectedType = localStore.types.find((type) => type.id === typeId) || localStore.types[0];
    localStore.q = q;
  }

  renderContent(): any {
    const typeChanged = (event: any, value: string) => {
      localStore.selectedType = localStore.types.find((type) => type.name === value) || localStore.types[0];
    }

    return (
        <div>
          <Paper>
            <Grid container spacing={2}>
              <Grid item xs={4}>
                <div className={"entity-type-select"}>
                  <Autocomplete
                      renderInput={
                        (params) =>
                            <TextField {...params}
                                       label="Entity type"
                                       size={"small"}
                            />}
                      options={localStore.types}
                      defaultValue={localStore.selectedType}
                      getOptionLabel={(type: EntityType) => type.name}
                      disableClearable={true}
                      onInputChange={typeChanged}
                  />
                </div>
              </Grid>
              <Grid item xs={8}>
                <div className="search-query-root">
                  <TextField
                      className="search-query-input"
                      id="outlined-basic"
                      placeholder="try <12> or <name != John> or <name ~ Sam and age = [35,40]> or press the (?) icon for details >>>"
                      label="search query"
                      fullWidth
                      size={"small"}/>
                  <IconButton aria-label="search" className="search-query-iconButton">
                    <SearchIcon/>
                  </IconButton>
                  <IconButton color="primary" className="search-query-iconButton" aria-label="directions">
                    <HelpIcon/>
                  </IconButton>
                </div>
              </Grid>
            </Grid>
          </Paper>
        </div>
    );
  }
}

export default DatabasePage;
