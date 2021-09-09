import React, {Component} from 'react';
import {EntityView} from '../../api/backend-types';
import {DatabaseApi} from '../../api/api';
import {observer} from 'mobx-react';
import EntityListView from './EntityListView';
import {Typography} from '@material-ui/core';

interface EntitiesListProps {
  entities: EntityView[],
  dbApi: DatabaseApi
}

@observer
class EntitiesList extends Component<EntitiesListProps> {

  render() {
    const entities = this.props.entities;
    return (
        <div>
          {(entities.length > 0) && entities.map((entity: EntityView) => (
              <div className={"entity-view-panel"} key={entity.id}>
                <EntityListView entity={entity} dbApi={this.props.dbApi}/>
              </div>
          ))}
          {(entities.length === 0) && <Typography style={{textAlign: 'center'}} variant={"h6"}>Nothing found</Typography>}
        </div>
    )
  }
}

export default EntitiesList;
