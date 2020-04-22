import React from "react";
import {observer} from 'mobx-react';
import BasePage from '../BasePage';
import {Grid, Typography} from "@material-ui/core";


@observer
class EmptyStore extends BasePage {

  pageId = 'empty-store';

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
              No stores configured. Start with adding some.
            </Typography>
          </Grid>
        </Grid>
    );
  }
}

export default EmptyStore;
