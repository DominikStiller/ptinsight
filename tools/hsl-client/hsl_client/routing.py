"""Client script for Routting GraphQL API"""
from gql import Client, gql
from gql.transport.requests import RequestsHTTPTransport
import json


API_BASE = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"


client = Client(transport=RequestsHTTPTransport(url=API_BASE))
res = client.execute(
    gql(
        """
{
  cancelledTripTimes(
    feeds: ["HSL"]
  ) {
    scheduledDeparture
    serviceDay
    trip {
      gtfsId
      tripHeadsign
      routeShortName
      directionId
      pattern {
        code
        name
      }
      route {
        gtfsId
        longName
      }
    }
    realtimeState
    headsign
  }
}
"""
    )
)

print(json.dumps(res, indent=2))
