"""Client script for GTFS-RT API used by Google Maps"""
import requests
import hsl_client.gtfs_realtime_pb2 as gtfs_realtime


r = requests.get("https://api.digitransit.fi/realtime/trip-updates/v2/hsl")
# r = requests.get('https://api.digitransit.fi/realtime/vehicle-positions/v2/hsl')
# r = requests.get('https://api.digitransit.fi/realtime/service-alerts/v2/hsl')

msg = gtfs_realtime.FeedMessage()
msg.ParseFromString(r.content)
print(msg)
