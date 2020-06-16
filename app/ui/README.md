# PT Insight: UI

The UI component is responsible for displaying the complex events detected in the processing component.
The backend receives messages from Kafka and sends them to the frontend via [Socket.IO](https://socket.io/). The
 backend also serves the frontend files, so only the backend needs to be started.


## Backend

### Prerequisites
* Python 3.8
* pipenv (`pip install pipenv`)


### Usage

Install dependencies (`pipenv install --dev`) and enter the pipenv (`pipenv shell`) before starting. Then execute with `python -m ptinsight.ui`.


### Configuration

Default: `config/ui.default.yaml`  
Local: `config/ui.yaml`  (create a copy of default)
Deployed: `../ansible/roles/ui-deploy/templates/ui.yaml.j2`


## Frontend

### Prerequisites
* Node.JS 12
* npm


### Development

Set up your development environment with `npm install` and start the Webpack bundler (to update the published assets
) using `npm run build-watch`.
