import React, {Component, ReactNode} from "react";
import {EntityLink, EntityView} from "../../api/backend-types";
import {
  Box,
  Button,
  ButtonGroup,
  Chip,
  ExpansionPanel, ExpansionPanelDetails, ExpansionPanelSummary,
  Grid, IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from "@material-ui/core";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import DeleteIcon from '@material-ui/icons/Delete';
import PencilIcon from '@material-ui/icons/Create';
import DehazeIcon from '@material-ui/icons/Dehaze';

interface EntityListViewProps {
  entity: EntityView
}

class EntityListView extends Component<EntityListViewProps> {

  state = {
    expanded: false
  }

  constructor(props: EntityListViewProps) {
    super(props)
  }

  private static renderProperties(entity: EntityView) {
    if (!entity.properties.length) {
      return (
          <div>
            No properties
          </div>
      )
    }
    return (<Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Name</TableCell>
          <TableCell align="right">Type</TableCell>
          <TableCell>Value</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {entity.properties.map((property) => (
            <TableRow key={property.name}>
              <TableCell component="th" scope="row">
                {property.name}
              </TableCell>
              <TableCell align="right">{property.type.displayName}</TableCell>
              <TableCell>{property.value}</TableCell>
            </TableRow>
        ))}
      </TableBody>
    </Table>)
  }

  private static renderLinksAndBlobs(entity: EntityView) {
    if (!entity.links.length) {
      return (<div>
        <Typography>Links</Typography>
        <div>No links</div>
      </div>)
    }
    return (
        <div>
          <Typography>Links</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Link</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {entity.links.map((link) => (
                  <TableRow key={link.name}>
                    <TableCell component="th" scope="row">
                      {link.name}
                    </TableCell>
                    <TableCell>
                      {link.entities.map((entityLink: EntityLink) => (
                          <Chip size="small" clickable label={entityLink.label}
                                color={entityLink.notExists ? "secondary" : "default"} variant={"outlined"}/>
                      ))}
                    </TableCell>
                  </TableRow>
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
          <Grid container spacing={2} className={"entity-list-view"}>
            <Grid container item xs={12}>
              <Grid item xs={8}>
                <Typography variant={"h6"}>{entity.label}</Typography>
              </Grid>
              <Grid item xs={4}>
                <ButtonGroup size="small" variant="outlined" color="primary"
                             aria-label="full-width contained primary button group"
                             className={"entity-list-view-actions"}
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
                {EntityListView.renderProperties(entity)}
              </Grid>
              {!expanded && <Button color={"primary"}
                                    size={"medium"}
                                    onClick={() => this.setState({expanded: true})}
                                    startIcon={<DehazeIcon/>}>expand
              </Button>}
              {expanded && EntityListView.renderLinksAndBlobs(entity)}
            </Grid>
          </Grid>
        </Paper>
    )
  }
}

export default EntityListView
