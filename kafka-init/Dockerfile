FROM landoop/fast-data-dev:latest

# Install necessary packages
RUN apt-get update && \
    apt-get install -y bash curl jq uuid-runtime && \
    rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy necessary files into /app
COPY . /app

# Make scripts executable
RUN chmod +x /app/init.sh /app/create-topic.sh /app/publish-sample-messages.sh

# Entry point for the container
ENTRYPOINT ["/app/init.sh"]