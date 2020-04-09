import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Button, Card, CardActionArea, CardActions, CardHeader, Chip, Fab} from "@material-ui/core";
import * as React from "react";
import {Component} from "react";
import store from "../store/store";
import AddIcon from "@material-ui/icons/Add";
import {Database, isHub, isYoutrack} from "../api/backend-types";
import api from "../api/api";
import {info, error} from "../components/notifications/notifications";
import {confirm} from "../components/confirmation/confirmation.store";

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

@observer
class DatabaseCard extends Component<DatabaseProps> {

  async open(db: Database) {
    db.opened = true;
    try {
      await api.system.startOrStop(db);
      info("database environement opened");
    } catch (e) {
      error("fail to open database environement");
      db.opened = false;
    }
  }

  async delete(db: Database) {
    confirm({
      text: "Close and remove " + db.location + "?",
      onConfirm: async () => {
        try {
          await api.system.deleteDB(db);
          info("database closed and removed");
          store.databases = store.databases.filter(value => value.uuid != db.uuid);
        } catch (e) {
          error("fail to close and remove database");
        }
      }
    })
  }

  async close(db: Database) {
    db.opened = false;
    try {
      await api.system.startOrStop(db);
      info("database environement closed");
    } catch (e) {
      error("fail to close database environement");
      db.opened = true;
    }
  }

  render(): React.ReactNode {
    const db = this.props.db;
    const title = ((isYoutrack(db) && "YouTrack") || (isHub(db) && "Hub") || db.key)

    return (
        <Card className={"database-card"}>
          <CardActionArea>
            <CardHeader
                title={title}
                subheader={<div>
                  <span>{db.location}</span>
                  <div className={"database-tags"}>
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
            <Button color="secondary" className={"remove-database-button"} onClick={() => this.delete(db)}>
              Close and Remove
            </Button>
          </CardActions>
          }
        </Card>
    )
  }
}


export default DatabasesPage;
