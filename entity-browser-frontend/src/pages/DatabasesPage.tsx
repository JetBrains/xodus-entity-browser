import {observer} from "mobx-react";
import BasePage from "./BasePage";
import {Card, CardHeader, Fab, Paper} from "@material-ui/core";
import {Link} from "react-router-dom";
import * as React from "react";
import store from "../store/store";
import AddIcon from "@material-ui/icons/Add";

@observer
class DatabasesPage extends BasePage<any> {

  pageId = 'databases';

  renderContent(): any {
    return (
        <div>
          {store.databases.map((database) => (
              <Paper>
                <Link
                    to={`/databases/${database.uuid}`}
                    key={database.uuid}
                >
                  <Card>
                    <CardHeader title={database.location}>
                    </CardHeader>
                  </Card>
                </Link>
              </Paper>
          ))}
          {!store.readonly && <Fab>
            <AddIcon/>
          </Fab>
          }
        </div>
    );
  }
}

export default DatabasesPage;
