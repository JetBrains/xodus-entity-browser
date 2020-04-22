import {inject, observer} from "mobx-react";
import BasePage from "./BasePage";
import {Button, Card, CardActionArea, CardActions, CardHeader, Chip, Fab} from "@material-ui/core";
import * as React from "react";
import {Component} from "react";
import store from "../store/store";
import AddIcon from "@material-ui/icons/Add";
import {Database, keyInfo} from "../api/backend-types";
import api from "../api/api";
import {error, info} from "../components/notifications/notifications";
import {confirm} from "../components/confirmation/confirmation.store";

@observer
class DatabasesPage extends BasePage {

  async doLoad(): Promise<void> {
    this.withTitle("Databases")
  }

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
@inject('routing')
class DatabaseCard extends Component<DatabaseProps> {

  private static async open(db: Database) {
    db.opened = true;
    try {
      await api.system.startOrStop(db);
      info("database environment opened");
    } catch (e) {
      error("fail to open database environment");
      db.opened = false;
    }
  }

  private static async delete(db: Database) {
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

  private static async close(db: Database) {
    db.opened = false;
    try {
      await api.system.startOrStop(db);
      info("database environement closed");
    } catch (e) {
      error("fail to close database environement");
      db.opened = true;
    }
  }

  private navigate() {
    // @ts-ignore
    this.props.routing.push("/databases/" + this.props.db.uuid);
  }

  render(): React.ReactNode {
    const db = this.props.db;
    const title = keyInfo(db);

    return (
        <Card className={"database-card"}>
          <CardActionArea>
            <CardHeader
                onClick={() => this.navigate()}
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
                      variant="outlined"
                      size="small"
                    />}
                  </div>
                </div>}
            />
          </CardActionArea>
          {!store.readonly && <CardActions>
            {db.opened && <Button color="primary" onClick={() => DatabaseCard.close(db)}>
              Close environment
            </Button>}
            {!db.opened && <Button color="primary" onClick={() => DatabaseCard.open(db)}>
              Open environment
            </Button>}
            <Button color="secondary" className={"remove-database-button"} onClick={() => DatabaseCard.delete(db)}>
              Close and Remove
            </Button>
          </CardActions>
          }
        </Card>
    )
  }
}


export default DatabasesPage;
