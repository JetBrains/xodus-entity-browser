import {FormSupport} from '../FormSupport';
import {Button, ButtonGroup, FormControlLabel, FormGroup, Grid, Switch, TextField} from '@material-ui/core';
import * as React from 'react';
import {Database} from '../../api/backend-types';
import {observer} from 'mobx-react';


export type DatabaseFormProps = {
  db: Database
}

export const YOUTRACK_KEY = 'teamsysstore';
export const HUB_KEY = 'jetPassServerDb';

@observer
export class DatabaseForm extends FormSupport<DatabaseFormProps> {

  data(): Database {
    return this.props.db;
  }


  render() {
    const db = this.props.db;

    const isYouTrackKey = db.key == YOUTRACK_KEY;
    const isHubKey = db.key == HUB_KEY;
    const isCustomKey = !isYouTrackKey && !isHubKey;
    return (
      <form noValidate name="database">
        <FormGroup>
          <TextField id="location" label="location" name="location" required={true}
                     ng-model="dbDialogCtrl.db.location"
                     value={db.location}
                     autoFocus
                     onChange={this.inputHandler('location')}
          />
        </FormGroup>
        <FormGroup>
          <Grid container>
            <Grid item xs={8}>
              <ButtonGroup style={{marginTop: 12}} color="primary" aria-label="outlined primary button group">
                <Button variant="contained" color={isYouTrackKey ? 'primary' : 'default'}
                        onClick={() => db.key = YOUTRACK_KEY}>YouTrack</Button>
                <Button variant="contained" color={isHubKey ? 'primary' : 'default'} onClick={() => db.key = HUB_KEY}>Hub</Button>
                <Button variant="contained" color={(!isYouTrackKey && !isHubKey) ? 'primary' : 'default'}
                        onClick={() => db.key = ''}>Other</Button>
              </ButtonGroup>
            </Grid>
            <Grid item xs={4}>
              <div>
                <TextField id="key"
                           name="key"
                           disabled={!isCustomKey}
                           label="key"
                           value={db.key}
                           onChange={this.inputHandler('key')}
                />
              </div>
            </Grid>
          </Grid>
        </FormGroup>
        <FormGroup>
          <FormControlLabel
            control={<Switch checked={db.opened} onChange={this.checkboxHandler('opened')}/>}
            label="open database"
          />
        </FormGroup>
        <FormGroup>
          <FormControlLabel
            control={<Switch checked={db.readonly} onChange={this.checkboxHandler('readonly')}/>}
            label="in readonly mode"
          />
        </FormGroup>
        <FormGroup>
          <FormControlLabel
            control={<Switch checked={db.watchReadonly} onChange={this.checkboxHandler('watchReadonly')}/>}
            label="watch external changes"
          />
        </FormGroup>
        <FormGroup>
          <Grid container>
            <Grid item xs={6}>
              <FormControlLabel
                control={<Switch checked={db.encrypted} onChange={this.checkboxHandler('encrypted')}/>}
                label="is encrypted"
              />
            </Grid>
            <Grid item xs={4}>
              {db.encrypted && <ButtonGroup color="primary" aria-label="outlined primary button group">
                <Button variant="contained" color={db.encryptionProvider === 'CHACHA' ? 'primary' : 'default'}
                        onClick={() => db.encryptionProvider = 'CHACHA'}>ChaCha</Button>
                <Button variant="contained" color={db.encryptionProvider === 'SALSA' ? 'primary' : 'default'}
                        onClick={() => db.encryptionProvider = 'SALSA'}>Salsa</Button>
              </ButtonGroup>}
            </Grid>
          </Grid>
        </FormGroup>
        {db.encrypted && <FormGroup>
          <TextField id="encryptionKey"
                     name="encryptionKey"
                     label="encryption key"
                     value={db.encryptionKey}
                     onChange={this.inputHandler('encryptionKey')}
          />
        </FormGroup>}
        {db.encrypted && <FormGroup>
          <TextField id="encryptionIV"
                     name="encryptionIV"
                     label="initialization vector"
                     value={db.encryptionIV}
                     onChange={this.inputHandler('encryptionIV')}
          />
        </FormGroup>}
      </form>
    );
  }
}
