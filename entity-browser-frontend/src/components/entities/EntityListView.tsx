import React, {Component} from 'react';
import {Database, EntityBlob, EntityLink, EntityView} from '../../api/backend-types';
import {
  Button,
  ButtonGroup,
  Chip,
  Grid, Link,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from '@material-ui/core';
import DeleteIcon from '@material-ui/icons/Delete';
import PencilIcon from '@material-ui/icons/Create';
import DehazeIcon from '@material-ui/icons/Dehaze';
import {withStyles} from '@material-ui/core/styles';
import {formatFileSize} from '../../api/file';
import api, {DatabaseApi} from '../../api/api';

const StyledTableCell = withStyles((theme) => ({
  head: {
    fontWeight: 'bold'
  },
  body: {
    fontSize: 14,
  },
}))(TableCell);

const StyledTableRow = withStyles((theme) => ({
  root: {
    '&:nth-of-type(odd)': {
      backgroundColor: theme.palette.action.hover,
    },
  },
}))(TableRow);


interface EntityListViewProps {
  entity: EntityView,
  dbApi: DatabaseApi
}

class EntityListView extends Component<EntityListViewProps> {

  state = {
    expanded: false
  }

  constructor(props: EntityListViewProps) {
    super(props)
  }

  renderProperties(entity: EntityView) {
    if (!entity.properties.length) {
      return (
        <div>
          No properties
        </div>
      )
    }
    return (<Table>
      <TableHead>
        <TableRow>
          <StyledTableCell width={'20%'}>Name</StyledTableCell>
          <StyledTableCell align="right" width={'10%'}>Type</StyledTableCell>
          <StyledTableCell width={'70%'}>Value</StyledTableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {entity.properties.map((property) => (
          <StyledTableRow key={entity.id + '_property_' + property.name}>
            <StyledTableCell component="th" scope="row">
              {property.name}
            </StyledTableCell>
            <StyledTableCell align="right">{property.type.displayName}</StyledTableCell>
            <StyledTableCell>{property.value}</StyledTableCell>
          </StyledTableRow>
        ))}
      </TableBody>
    </Table>)
  }

  renderLinks(entity: EntityView) {
    if (!entity.links.length) {
      return (<div>
        <Typography>Links</Typography>
        <div>No links</div>
      </div>)
    }
    return (
      <div>
        <Typography>Links</Typography>
        <Table>
          <TableHead>
            <TableRow>
              <StyledTableCell width={'30%'}>Name</StyledTableCell>
              <StyledTableCell width={'70%'}>Link</StyledTableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entity.links.map((link) => (
              <StyledTableRow key={entity.id + '_link_' + link.name}>
                <StyledTableCell component="th" scope="row">
                  {link.name}
                </StyledTableCell>
                <StyledTableCell>
                  {link.entities.map((entityLink: EntityLink) => (
                    <Chip size="small" clickable label={entityLink.label}
                          color={entityLink.notExists ? 'secondary' : 'default'} variant={'outlined'}/>
                  ))}
                </StyledTableCell>
              </StyledTableRow>
            ))}
          </TableBody>
        </Table>
      </div>)
  }

  private renderBlobs(entity: EntityView) {
    const {dbApi} = this.props;
    if (!entity.blobs.length) {
      return (<div>
        <Typography>No blobs</Typography>
      </div>)
    }

    const onDownloadBlob = async (blob: EntityBlob, asString: boolean) => {
      await dbApi.downloadBlob(entity.id, blob.name, asString);
    };

    return (
      <div>
        <Typography>Blobs</Typography>
        <Table>
          <TableHead>
            <TableRow>
              <StyledTableCell width={'30%'}>Name</StyledTableCell>
              <StyledTableCell width={'70%'}>Blob</StyledTableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entity.blobs.map((blob) => (
              <StyledTableRow key={entity.id + '_blob_' + blob.name}>
                <StyledTableCell component="th" scope="row">
                  {blob.name}
                </StyledTableCell>
                <StyledTableCell>
                  Download {formatFileSize(blob.blobSize)}
                  <ButtonGroup>
                    <Button onClick={async () => onDownloadBlob(blob, false)}>Binary</Button>
                    <Button onClick={async () => onDownloadBlob(blob, true)}>UTF</Button>
                  </ButtonGroup>
                </StyledTableCell>
              </StyledTableRow>
            ))}
          </TableBody>
        </Table>
      </div>)
  }

  render() {
    const entity = this.props.entity
    const expanded = this.state.expanded;
    return (
      <Paper key={entity.id}>
        <Grid container spacing={2} className={'entity-list-view'}>
          <Grid item xs={8}>
            <Typography variant={'h6'}>{entity.label}</Typography>
          </Grid>
          <Grid item xs={4}>
            <ButtonGroup size="small" variant="outlined" color="primary"
                         aria-label="full-width contained primary button group"
                         className={'entity-list-view-actions'}
            >
              <Button aria-label="delete" size="small">
                <PencilIcon fontSize="small"/>
              </Button>
              <Button aria-label="delete" size="small">
                <DeleteIcon fontSize="small"/>
              </Button>
            </ButtonGroup>
          </Grid>
          <Grid item xs={12}>
            {this.renderProperties(entity)}
          </Grid>
          <Grid item xs={12}>
            {expanded && this.renderLinks(entity)}
            {expanded && this.renderBlobs(entity)}
            {!expanded && <Button color={'primary'}
                                  size={'medium'}
                                  onClick={() => this.setState({expanded: true})}
                                  startIcon={<DehazeIcon/>}>expand
            </Button>}
          </Grid>
        </Grid>
      </Paper>
    )
  }
}

export default EntityListView
