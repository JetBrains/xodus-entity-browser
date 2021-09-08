import {Database, keyInfo} from '../../api/backend-types';
import {inject, observer} from 'mobx-react';
import {Component} from 'react';
import api from '../../api/api';
import {error, info} from '../notifications/notifications';
import {confirm} from '../confirmation/confirmation.store';
import store from '../../store/store';
import * as React from 'react';
import {Button, Card, CardActionArea, CardActions, CardHeader, Chip} from '@material-ui/core';

export type DatabaseProps = {
  db: Database
}

@observer
@inject('routing')
export class DatabaseCard extends Component<DatabaseProps> {

  async open(db: Database) {
    db.opened = true;
    try {
      await api.system.startOrStop(db);
      info('database environment opened');
    } catch (e) {
      error('fail to open database environment');
      db.opened = false;
    }
  }

  delete(db: Database) {
    confirm({
      text: 'Close and remove ' + db.location + '?',
      onConfirm: async () => {
        try {
          await api.system.deleteDB(db);
          info('database closed and removed');
          store.databases = store.databases.filter(value => value.uuid != db.uuid);
        } catch (e) {
          error('fail to close and remove database');
        }
      }
    })
  }

  async close(db: Database) {
    db.opened = false;
    try {
      await api.system.startOrStop(db);
      info('database environement closed');
    } catch (e) {
      error('fail to close database environement');
      db.opened = true;
    }
  }

  private navigate() {
    // @ts-ignore
    this.props.routing.push('/databases/' + this.props.db.uuid);
  }

  render(): React.ReactNode {
    const db = this.props.db;
    const title = keyInfo(db);

    return (
      <Card className={'database-card'}>
        <CardActionArea>
          <CardHeader
            onClick={() => this.navigate()}
            title={title}
            subheader={<div>
              <span>{db.location}</span>
              <div className={'database-tags'}>
                {db.readonly && <Chip
                  label="Readonly"
                  color="primary"
                  size="small"
                />}
                {db.encrypted && <Chip
                  label="Encrypted"
                  color="secondary"
                  size="small"
                />}
                {!db.opened && <Chip
                  label="Closed"
                  variant="outlined"
                  size="small"
                />}
              </div>
            </div>}
          />
        </CardActionArea>
        {!store.readonly && <CardActions>
          {db.opened && <Button color="primary" onClick={() => this.close(db)}>
            Close environment
          </Button>}
          {!db.opened && <Button color="primary" onClick={() => this.open(db)}>
            Open environment
          </Button>}
          <Button color="secondary" className={'remove-database-button'} onClick={() => this.delete(db)}>
            Close and Remove
          </Button>
        </CardActions>
        }
      </Card>
    )
  }
}
