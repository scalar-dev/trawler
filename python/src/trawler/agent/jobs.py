import yaml
from typing import List, Literal, Optional, TypedDict
from copy import deepcopy
import schedule
import time
from trawler.sql.extract import extract_sql

JobType = Literal['sql']


class Schedule(TypedDict):
  every: str
  interval: int = 1
  at: Optional[str]

class Job(TypedDict):
  schedule: Schedule
  type: JobType

class SQLJob(TypedDict):
  uri: str
  override_dbname: Optional[str]

class Config(TypedDict):
  jobs: List[Job]


def read_config(fname: str) -> Config:
  with open(fname) as f:
    config = yaml.load(f, Loader=yaml.FullLoader)

    return config


def run_scheduler(fname: str):
  config = read_config(fname)

  for job in config["jobs"]:
    scheduled_job = getattr(
      schedule.every(job["schedule"]["interval"]),
      job["schedule"]["every"]
    )
    if job["schedule"].get("at"):
      scheduled_job.at(job["schedule"]["at"])

    if job["type"] == "sql":
      args = deepcopy(job)
      args.pop("schedule")
      args.pop("type")
      scheduled_job.do(lambda: extract_sql(**args))

  print(f"Scheduled {len(schedule.get_jobs())} jobs")

  while True:
    schedule.run_pending()
    time.sleep(1)
