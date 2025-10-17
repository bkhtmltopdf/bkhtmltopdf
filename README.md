## bkhtmltopdf

bkhtmltopdf is an open-source (LGPLv3) `HTML-to-PDF` program that can automatically convert HTML into PDF files.

bkhtmltopdf uses the [Chromium](https://www.chromium.org) Blink rendering engine. For community version deployments, a
display server is required. If you need “headless” operation, please purchase the [enterprise version](https://bkhtmltopdf.com/pricing).

See https://bkhtmltopdf.com for updated documentation.

## Online

This online website supports HTML code input for PDF generation. You can enter any HTML,
CSS, and JavaScript code.

See https://demo.bkhtmltopdf.com

## Performance

bkhtmltopdf delivers exceptional performance, rendering a 10-page PDF takes only about **60 milliseconds**.

See https://bkhtmltopdf.com/performance

## Deployment

**Community Version**: Licensed under **LGPLv3**

```shell
JDK21/bin/java -jar bkhtmltopdf-x.y.z.jar
```

**Enterprise Version**: For **evaluation purposes only**

```shell
docker run --shm-size=256mb -it --rm -p 8080:8080 bkhtmltopdf/bkhtmltopdf-ee:latest
```