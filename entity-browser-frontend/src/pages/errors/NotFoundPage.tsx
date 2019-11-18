import React from "react";
import {observer} from 'mobx-react';
import BasePage from '../BasePage';
import {Grid, Typography} from "@material-ui/core";


@observer
class NotFoundPage extends BasePage<{}> {

  pageId = 'users-not-found';

  get pageTitle() {
    return 'Not found';
  }

  renderContent(): any {
    return (
        <Grid
            container
            spacing={0}
            direction="column"
            alignItems="center"
            justify="center"
            style={{minHeight: '100vh'}}
        >

          <Grid item xs={3}>
            <Typography gutterBottom variant="subtitle1">
              Not found
            </Typography>
          </Grid>
        </Grid>
    );
  }
}

export default NotFoundPage;
