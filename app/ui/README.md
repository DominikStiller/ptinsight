# PT Insight: UI

The UI component is responsible for displaying the complex events detected in the processing component.
The backend receives messages from Kafka and sends them to the frontend via [Socket.IO](https://socket.io/).



## Prerequisites
* Python 3.8
* pipenv (`pip install pipenv`)


## Usage

Install dependencies (`pipenv install --dev`) and enter the pipenv (`pipenv shell`) before starting. Then execute with `python -m ptinsight.ui`.


## Configuration

Default: `config/ui.default.yaml`  
Local: `config/ui.yaml`  (create a copy of default)
Deployed: `../ansible/roles/ui-deploy/templates/ui.yaml.j2`
