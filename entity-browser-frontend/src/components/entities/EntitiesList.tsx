import React, {Component} from "react";
import {EntityView} from "../../api/backend-types";
import {DatabaseApi} from "../../api/api";
import {observer} from "mobx-react";
import EntityListView from "./EntityListView";

interface EntitiesListProps {
  entities: EntityView[],
  dbApi: DatabaseApi
}

@observer
class EntitiesList extends Component<EntitiesListProps> {

  render() {
    return (
        <div>
          {/*<Paper className={"search-pagination-row"}>*/}
          {/*  <Grid container spacing={0} direction={"row"}>*/}
          {/*    <Grid item xs={4}/>*/}
          {/*    <Grid item xs={5}>*/}
          {/*    </Grid>*/}
          {/*  </Grid>*/}
          {/*</Paper>*/}
          {this.props.entities.map((entity: EntityView) => (
              <div className={"entity-view-panel"} key={entity.id}>
                <EntityListView entity={entity}>
                </EntityListView>
              </div>
          ))}
          {/*<AppBar position="fixed" color="primary" style={{top: 'auto', bottom: 0}}>*/}
          {/*  <Toolbar>*/}
          {/*    {pages > 1 && <Pagination count={pages} variant={"outlined"}/>}*/}
          {/*    <Typography variant={"h6"}*/}
          {/*                className={"search-total-number"}>Total: {entitiesListStore.totalCount}</Typography>*/}

          {/*  </Toolbar>*/}
          {/*</AppBar>*/}
        </div>
    )
  }
}

export default EntitiesList;
