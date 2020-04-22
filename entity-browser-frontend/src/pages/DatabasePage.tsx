import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Database, EntityType, keyInfo} from "../api/backend-types";
import store from "../store/store";
import {Fab, Grid, LinearProgress, Paper, TextField} from "@material-ui/core";
import * as React from "react";
import {KeyboardEvent} from "react";
import {observable} from "mobx";
import api, {DatabaseApi} from "../api/api";
import * as queryString from "querystring";
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import AddIcon from '@material-ui/icons/Add';
import HelpIcon from '@material-ui/icons/Help';
import Autocomplete from '@material-ui/lab/Autocomplete';
import EntitiesList from "../components/entities/EntitiesList";


class DatabasePageStore {
  @observable database: Database = store.databases[0];
  @observable types: EntityType[] = [];
  @observable q: string = '';
  @observable tempQ: string = '';
  @observable loading: boolean = true;

  // @ts-ignore
  @observable selectedType: EntityType = {};
  // @ts-ignore
  api: DatabaseApi = {}

  reset() {
    this.database = store.databases[0];
    this.types = [];
    this.q = '';
    this.tempQ = '';
    this.loading = true;
    // @ts-ignore
    this.selectedType = {};
    // @ts-ignore
    this.api = {}
  }
}

const databaseStore = new DatabasePageStore();

@observer
class DatabasePage extends BasePage {

  constructor(props: any) {
    super(props);
    databaseStore.reset();
  }

  async componentDidMount(): Promise<void> {
    // @ts-ignore
    const id = this.props.match.params.databaseId;

    if (id) {
      databaseStore.database = store.databases.filter((it) => it.uuid === id)[0];
    } else {
      databaseStore.database = store.databases[0];
    }
    databaseStore.api = api.database(databaseStore.database);

    this.withTitle(keyInfo(databaseStore.database) + " " + databaseStore.database.location);

    await this.setupFromQueryParams();
    databaseStore.loading = false;
    return super.componentDidMount();
  }

  async setupFromQueryParams() {
    const holdMyBeer = (param: string | string[], def: string) => (param || def).toString();

    let search = this.props.location.search;
    const params = queryString.parse(search ? search.substring(1, search.length) : search);
    const typeId = parseInt(holdMyBeer(params.typeId, "-1"));
    const q = holdMyBeer(params.q, "");

    databaseStore.types = await databaseStore.api.entityTypes();
    databaseStore.selectedType = databaseStore.types.find((type) => type.id === typeId) || databaseStore.types[0];
    databaseStore.q = q;
    databaseStore.tempQ = q;
  }

  syncWithQueryParams() {
    this.props.history.push({
      pathname: this.props.location.pathname,
      search: `?typeId=${databaseStore.selectedType.id}&q=${databaseStore.q}`
    });
  }

  renderContent(): any {
    if (databaseStore.loading) {
      return (<LinearProgress/>)
    }

    const typeChanged = (event: any, value: string) => {
      databaseStore.selectedType = databaseStore.types.find((type) => type.name === value) || databaseStore.types[0];
      this.syncWithQueryParams();
    }

    const qChanged = () => {
      databaseStore.q = databaseStore.tempQ;
      this.syncWithQueryParams();
    }

    const onEnter = (e: KeyboardEvent) => {
      if (e.key === 'Enter') {
        qChanged();
      }
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
                      options={databaseStore.types}
                      defaultValue={databaseStore.selectedType}
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
                      value={databaseStore.tempQ}
                      onChange={(event) => databaseStore.tempQ = event.target.value}
                      onKeyDown={onEnter}
                      fullWidth
                      size={"small"}/>
                  <IconButton aria-label="search" className="search-query-iconButton" onClick={qChanged}>
                    <SearchIcon/>
                  </IconButton>
                  <IconButton color="primary" className="search-query-iconButton" aria-label="directions">
                    <HelpIcon/>
                  </IconButton>
                </div>
              </Grid>
            </Grid>
          </Paper>
          <Fab color={"secondary"}>
            <AddIcon/>
          </Fab>
          <EntitiesList
              q={databaseStore.q}
              typeId={databaseStore.selectedType.id}
              dbApi={databaseStore.api}
          />
        </div>
    );
  }
}

export default DatabasePage;
