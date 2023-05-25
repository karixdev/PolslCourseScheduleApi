# PolslCourseScheduleApi

## Project description

The aim of the project was to build a publicly accessible course schedule REST API for Silesian University of Technology's full-time majors. This is a private project, not supported by the university. The motivation was precisely the lack of a publicly available REST API of the university's course schedule - project web scrapes schedules from [plan.polsl.pl](https://plan.polsl.pl/). The second factor was that the official schedule page works quite slowly - every time we request for another schedule half of the page is re-rendered and all front-end processing (assigning classes to HTML elements, etc.) is done on the server side, not on the client side.
The course schedules are cyclically updated - by default every hour.