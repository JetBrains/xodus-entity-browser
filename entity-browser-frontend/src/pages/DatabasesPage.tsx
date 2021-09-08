import {observer} from 'mobx-react';
import BasePage from './BasePage';
import {Button, Dialog, DialogActions, DialogContent, DialogTitle, Fab} from '@material-ui/core';
import * as React from 'react';
import store from '../store/store';
import AddIcon from '@material-ui/icons/Add';
import {Database} from '../api/backend-types';
import {action, observable} from 'mobx';
import {DatabaseForm} from '../components/db/DatabaseForm';
import {DatabaseCard} from '../components/db/DatabaseCard';
import api from '../api/api';
import {info} from '../components/notifications/notifications';


class DatabasePageStore {

  @observable showNewDatabase: boolean = false;
  @observable customKey: boolean = false;
  // @ts-ignore
  @observable newDatabase: Database = {};

  @action
  openNewDatabaseDialog() {
    this.showNewDatabase = true;
    this.customKey = false;
    // @ts-ignore
    this.newDatabase = {};
  }

  @action
  closeNewDatabase() {
    this.showNewDatabase = false;
  }
}

const databasePageStore = new DatabasePageStore();


@observer
class DatabasesPage extends BasePage {

  async doLoad(): Promise<void> {
    this.withTitle('Databases')
  }

  async addDatabase() {
    try {
      const db = await api.system.newDB(databasePageStore.newDatabase);
      info('Database added');
      store.databases.push(db);
      databasePageStore.closeNewDatabase();
    } catch (e) {
      api.errorHandler(e);
    }
  }

  renderContent(): any {
    const newDatabase = () => databasePageStore.openNewDatabaseDialog();
    const closeNewDatabase = () => databasePageStore.closeNewDatabase();

    return (
      <div>
        {store.databases.map((database) => (
          <DatabaseCard db={database} key={database.uuid}/>
        ))}
        {!store.readonly && <Fab color="secondary">
          <AddIcon onClick={newDatabase}/>
        </Fab>
        }
        <Dialog open={databasePageStore.showNewDatabase}>
          <DialogTitle>Open new database</DialogTitle>
          <DialogContent>
            <DatabaseForm db={databasePageStore.newDatabase}/>
          </DialogContent>
          <DialogActions>
            <Button data-ng-click="dbDialogCtrl.cancel()" onClick={closeNewDatabase}>Cancel</Button>
            <Button variant={'contained'} color={'primary'} onClick={async () => this.addDatabase()}>Add</Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }

}

export default DatabasesPage;
