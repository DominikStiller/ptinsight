# PT Insight: Visualization

The visualization component is responsible for displaying the complex events detected in the processing component.
The backend receives messages from Kafka and sends them to the frontend via [Socket.IO](https://socket.io/). The
 backend also serves the frontend files, so only the backend needs to be started.


## Backend

### Prerequisites
* Python 3.8
* poetry


### Usage

Install dependencies (`poetry install`) and enter the venv (`poetry shell`) before starting. Then execute with `python -m ptinsight.visualization`.


### Configuration

Default: `config/visualization.default.yaml`  
Local: `config/visualization.yaml`  (create a copy of default)  
Deployed: `../ansible/roles/visualization-deploy/templates/visualization.yaml.j2`


## Frontend

### Prerequisites
* Node.JS 12
* npm


### Development

Set up your development environment with `npm install` and start the Webpack bundler (to update the published assets) using `npm run build-watch`.
