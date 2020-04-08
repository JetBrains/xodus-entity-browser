import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Button, Card, CardContent, CardHeader, Chip, Fab} from "@material-ui/core";
import * as React from "react";
import {Component} from "react";
import store from "../store/store";
import AddIcon from "@material-ui/icons/Add";
import {Database, isHub, isYoutrack} from "../api/backend-types";
import ActionsDropdown from "../components/actions-dropdown/ActionsDropdown";

@observer
class DatabasesPage extends BasePage<any> {

  pageId = 'databases';

  renderContent(): any {
    return (
        <div>
          {store.databases.map((database) => (
              <DatabaseCard db={database} key={database.uuid}/>
          ))}
          {!store.readonly && <Fab color="secondary">
            <AddIcon/>
          </Fab>
          }
        </div>
    );
  }
}

type DatabaseProps = {
  db: Database
}

class DatabaseCard extends Component<DatabaseProps> {

  async open(db: Database) {

  }

  async close(db: Database) {

  }

  render(): React.ReactNode {
    const db = this.props.db;

    const title = ((isYoutrack(db) && "YouTrack") || (isHub(db) && "Hub") || db.key)

    const menuActions = [];

    if (db.opened) {
      menuActions.push({
        title: 'Close environment',
        action: () => this.close(db)
      });
    }

    if (!db.opened) {
      menuActions.push({
        title: 'Open environment',
        action: () => this.open(db)
      });
    }

    return (
        <Card className={"database-card"}>
          <CardHeader
              action={
                !store.readonly && <ActionsDropdown actions={menuActions}/>
              }
              title={title}
              subheader={db.location}
          />

          <CardContent>
            {db.readonly && <Chip
              label="Readonly"
              color="primary"
            />}
            {db.encrypted && <Chip
              label="Encrypted"
              color="secondary"
            />}
            {!store.readonly && <Button variant="contained" color="primary" className={"remove-database-button"}>
              Close and Remove
            </Button>}
          </CardContent>
        </Card>
    )
  }
}


export default DatabasesPage;
