import React, {Component} from "react";
import {EntityView} from "../../api/backend-types";
import {DatabaseApi, PAGE_SIZE} from "../../api/api";
import {observer} from "mobx-react";
import {Grid, LinearProgress, Paper, Typography} from "@material-ui/core";
import {observable} from "mobx";
import {error} from "../notifications/notifications";
import {Pagination} from "@material-ui/lab";
import EntityListView from "./EntityListView";

interface EntitiesListProps {
  q: string,
  typeId: number,
  dbApi: DatabaseApi
}

class EntitiesListStore {
  @observable loading: boolean = true
  @observable entities: EntityView[] = []
  @observable totalCount: number = 0

  reset() {
    this.loading = true
    this.entities = []
    this.totalCount = 0
  }
}

const entitiesListStore = new EntitiesListStore();

@observer
class EntitiesList extends Component<EntitiesListProps> {

  constructor(props: EntitiesListProps) {
    super(props)
    entitiesListStore.reset()
  }

  async componentDidUpdate(prevProps: Readonly<EntitiesListProps>, prevState: Readonly<{}>, snapshot?: any): Promise<void> {
    if (prevProps.typeId !== this.props.typeId || prevProps.q !== this.props.q) {
      await this.componentDidMount()
    }
  }

  async componentDidMount(): Promise<void> {
    const {q, typeId, dbApi} = this.props
    try {
      entitiesListStore.loading = true
      let pager = await dbApi.searchEntities(q, typeId)
      entitiesListStore.entities = pager.items
      entitiesListStore.totalCount = pager.totalCount
      entitiesListStore.loading = false
    } catch (e) {
      error("Can't fetch entities: " + e)
    }
  }

  render() {
    if (entitiesListStore.loading) {
      return (
          <LinearProgress/>
      )
    }
    const pages = Math.ceil(Math.max(entitiesListStore.totalCount / PAGE_SIZE, 1))
    return (
        <div>
          <Paper className={"search-pagination-row"}>
            <Grid container spacing={0} direction={"row"}>
              <Grid item xs={4}/>
              <Grid item xs={5}>
                {pages > 1 && <Pagination count={pages} showFirstButton/>}
              </Grid>
              <Grid item xs={3}>
                <Typography variant={"h6"}
                            className={"search-total-number"}>Total: {entitiesListStore.totalCount}</Typography>
              </Grid>
            </Grid>
          </Paper>
          {entitiesListStore.entities.map((entity: EntityView) => (
              <div className={"entity-view-panel"} key={entity.id}>
                <EntityListView entity={entity}>
                </EntityListView>
              </div>
          ))}
        </div>
    )
  }
}

export default EntitiesList;
