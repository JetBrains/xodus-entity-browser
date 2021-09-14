import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Database, EntityType, EntityView, keyInfo} from "../api/backend-types";
import store from "../store/store";
import {Fab, Grid, LinearProgress, Paper, TextField, Typography} from "@material-ui/core";
import * as React from "react";
import {KeyboardEvent} from "react";
import {observable} from "mobx";
import api, {DatabaseApi, PAGE_SIZE} from "../api/api";
import * as queryString from "querystring";
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';
import AddIcon from '@material-ui/icons/Add';
import HelpIcon from '@material-ui/icons/Help';
import Autocomplete from '@material-ui/lab/Autocomplete';
import EntitiesList from "../components/entities/EntitiesList";
import {error} from "../components/notifications/notifications";
import {Pagination} from "@material-ui/lab";


class EntitiesPager {
  @observable entities: EntityView[] = []
  @observable total: number = 0
}

class DatabasePageStore {

  @observable database: Database = store.databases[0];
  @observable types: EntityType[] = [];
  @observable q: string = '';
  @observable page: number = 0;
  @observable tempQ: string = '';
  @observable loading: boolean = true;

  @observable pager: EntitiesPager = new EntitiesPager()
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
    this.pager = new EntitiesPager()
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
    await this.searchEntities();
    databaseStore.loading = false;
    return super.componentDidMount();
  }

  async setupFromQueryParams() {
    const holdMyBeer = (param: string | string[], def: string) => (param || def).toString();

    let search = this.props.location.search
    const params = queryString.parse(search ? search.substring(1, search.length) : search)
    const typeId = parseInt(holdMyBeer(params.typeId, "-1"))
    const q = holdMyBeer(params.q, "")

    databaseStore.types = await databaseStore.api.entityTypes()
    databaseStore.selectedType = databaseStore.types.find((type) => type.id === typeId) || databaseStore.types[0]
    databaseStore.q = q
    databaseStore.tempQ = q
    databaseStore.page = parseInt(holdMyBeer(params.page, "0"))
  }

  async syncWithQueryParams() {
    this.props.history.push({
      pathname: this.props.location.pathname,
      search: `?typeId=${databaseStore.selectedType.id}&q=${databaseStore.q}&page=${databaseStore.page}`
    });
    await this.searchEntities();
  }

  async goToPage(page: number) {
    databaseStore.page = page;
    this.props.history.push({
      pathname: this.props.location.pathname,
      search: `?typeId=${databaseStore.selectedType.id}&q=${databaseStore.q}&page=${databaseStore.page}`
    });
    await this.searchEntities();
  }

  renderHeaderPlugin() {
    const total = databaseStore.pager.total
    if (total >= 1) {
      const pages = Math.ceil(Math.max(total / PAGE_SIZE, 1))
      return (<div className={"search-counter"}>
            {pages > 1 && <Pagination count={pages} variant={"outlined"} onChange={async (event, page) => this.goToPage(page)}/>}
            <Typography variant={"h6"}
                        className={"search-total-number"}>Total: {total}</Typography>
          </div>
      )
    }
    return super.renderHeaderPlugin()
  }

  async searchEntities(): Promise<void> {
    const {q, selectedType, api} = databaseStore
    try {
      databaseStore.loading = true
      const pager = await api.searchEntities(q, selectedType.id, databaseStore.page)

      databaseStore.pager.entities = pager.items
      databaseStore.pager.total = pager.totalCount
      databaseStore.loading = false
    } catch (e) {
      error("Can't fetch entities: " + e)
    }
  }


  renderContent(): any {
    if (databaseStore.loading) {
      return (<LinearProgress/>)
    }

    const typeChanged = async (event: any, value: EntityType | null) => {
      if (value && value.id !== databaseStore.selectedType.id) {
        databaseStore.selectedType = value || databaseStore.types[0];
        await this.syncWithQueryParams();
      }
    }

    const qChanged = async () => {
      databaseStore.q = databaseStore.tempQ;
      await this.syncWithQueryParams();
    }

    const onEnter = async (e: KeyboardEvent) => {
      if (e.key === 'Enter') {
        await qChanged();
      }
    }

    return (
        <div>
          <Paper>
            <Grid container spacing={2} style={{marginBottom: 20}}>
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
                      onChange={typeChanged}
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
          <EntitiesList entities={databaseStore.pager.entities} dbApi={databaseStore.api}/>
        </div>
    );
  }
}

export default DatabasePage;
